#!/usr/bin/env node

/**
 * Script hỗ trợ build và deploy
 * Sử dụng: node build-and-deploy.js [options]
 * 
 * Options:
 *   --check        Chỉ kiểm tra, không build
 *   --preview      Build và chạy preview
 *   --clean        Xóa thư mục dist trước khi build
 */

import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const args = process.argv.slice(2);
const shouldCheck = args.includes('--check');
const shouldPreview = args.includes('--preview');
const shouldClean = args.includes('--clean');

console.log('🚀 Build and Deploy Helper\n');

// Kiểm tra Node.js version
function checkNodeVersion() {
  const nodeVersion = process.version;
  const majorVersion = parseInt(nodeVersion.slice(1).split('.')[0]);
  
  if (majorVersion < 16) {
    console.error('❌ Node.js version phải >= 16.x');
    console.error(`   Hiện tại: ${nodeVersion}`);
    process.exit(1);
  }
  
  console.log(`✅ Node.js version: ${nodeVersion}`);
}

// Kiểm tra dependencies
function checkDependencies() {
  const packageJsonPath = path.join(__dirname, 'package.json');
  const nodeModulesPath = path.join(__dirname, 'node_modules');
  
  if (!fs.existsSync(packageJsonPath)) {
    console.error('❌ Không tìm thấy package.json');
    process.exit(1);
  }
  
  if (!fs.existsSync(nodeModulesPath)) {
    console.warn('⚠️  Thư mục node_modules không tồn tại');
    console.log('   Chạy: npm install');
    process.exit(1);
  }
  
  console.log('✅ Dependencies đã được cài đặt');
}

// Kiểm tra file môi trường
function checkEnvFiles() {
  const envFiles = [
    'env.production',
    'env.local'
  ];
  
  let hasEnv = false;
  envFiles.forEach(file => {
    if (fs.existsSync(path.join(__dirname, file))) {
      console.log(`✅ Tìm thấy ${file}`);
      hasEnv = true;
    }
  });
  
  if (!hasEnv) {
    console.warn('⚠️  Không tìm thấy file môi trường (env.production hoặc env.local)');
    console.warn('   Ứng dụng sẽ sử dụng giá trị mặc định');
  }
}

// Xóa thư mục dist
function cleanDist() {
  const distPath = path.join(__dirname, 'dist');
  if (fs.existsSync(distPath)) {
    console.log('🧹 Đang xóa thư mục dist...');
    fs.rmSync(distPath, { recursive: true, force: true });
    console.log('✅ Đã xóa thư mục dist');
  }
}

// Build project
function buildProject() {
  console.log('\n📦 Đang build project...');
  try {
    execSync('npm run build:prod', { 
      stdio: 'inherit',
      cwd: __dirname 
    });
    console.log('✅ Build thành công!\n');
  } catch (error) {
    console.error('❌ Build thất bại!');
    process.exit(1);
  }
}

// Kiểm tra thư mục dist
function checkDist() {
  const distPath = path.join(__dirname, 'dist');
  
  if (!fs.existsSync(distPath)) {
    console.error('❌ Thư mục dist không tồn tại');
    process.exit(1);
  }
  
  const requiredFiles = [
    'index.html',
    'assets'
  ];
  
  console.log('\n📋 Kiểm tra thư mục dist:');
  
  requiredFiles.forEach(file => {
    const filePath = path.join(distPath, file);
    if (fs.existsSync(filePath)) {
      const stats = fs.statSync(filePath);
      if (stats.isDirectory()) {
        const files = fs.readdirSync(filePath);
        console.log(`✅ ${file}/ (${files.length} files)`);
      } else {
        const size = (stats.size / 1024).toFixed(2);
        console.log(`✅ ${file} (${size} KB)`);
      }
    } else {
      console.error(`❌ Thiếu file: ${file}`);
    }
  });
  
  // Kiểm tra kích thước assets
  const assetsPath = path.join(distPath, 'assets');
  if (fs.existsSync(assetsPath)) {
    const assets = fs.readdirSync(assetsPath);
    let totalSize = 0;
    
    assets.forEach(asset => {
      const assetPath = path.join(assetsPath, asset);
      const stats = fs.statSync(assetPath);
      totalSize += stats.size;
    });
    
    const totalSizeMB = (totalSize / 1024 / 1024).toFixed(2);
    console.log(`\n📊 Tổng kích thước assets: ${totalSizeMB} MB`);
    
    if (totalSize > 5 * 1024 * 1024) {
      console.warn('⚠️  Kích thước assets khá lớn (>5MB), nên kiểm tra lại');
    }
  }
}

// Tạo file cấu hình deploy
function createDeployFiles() {
  console.log('\n📝 Đang tạo file cấu hình deploy...');
  
  try {
    execSync('node deploy.js', { 
      stdio: 'inherit',
      cwd: __dirname 
    });
  } catch (error) {
    console.error('❌ Không thể tạo file cấu hình deploy');
    console.error('   Bạn có thể tạo thủ công theo hướng dẫn trong HUONG_DAN_BUILD_DEPLOY.md');
  }
}

// Chạy preview
function runPreview() {
  console.log('\n👀 Đang khởi động preview server...');
  console.log('   Truy cập: http://localhost:4173');
  console.log('   Nhấn Ctrl+C để dừng\n');
  
  try {
    execSync('npm run preview', { 
      stdio: 'inherit',
      cwd: __dirname 
    });
  } catch (error) {
    // Preview server sẽ chạy cho đến khi user dừng
  }
}

// Main function
function main() {
  // Kiểm tra môi trường
  checkNodeVersion();
  checkDependencies();
  checkEnvFiles();
  
  if (shouldCheck) {
    console.log('\n✅ Kiểm tra hoàn tất!');
    return;
  }
  
  // Xóa dist nếu cần
  if (shouldClean) {
    cleanDist();
  }
  
  // Build project
  buildProject();
  
  // Kiểm tra dist
  checkDist();
  
  // Tạo file cấu hình deploy
  createDeployFiles();
  
  // Chạy preview nếu cần
  if (shouldPreview) {
    runPreview();
  } else {
    console.log('\n🎉 Build hoàn tất!');
    console.log('\n📋 Các bước tiếp theo:');
    console.log('1. Kiểm tra thư mục dist/');
    console.log('2. Upload nội dung dist/ lên server');
    console.log('3. Kiểm tra cấu hình server (.htaccess hoặc web.config)');
    console.log('4. Test website trên server');
    console.log('\n💡 Chạy "node build-and-deploy.js --preview" để xem preview');
  }
}

main();

