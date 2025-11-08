#!/usr/bin/env node

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

console.log('🚀 Preparing deployment files...');

// Copy .htaccess to dist folder
const htaccessContent = `Options -MultiViews
RewriteEngine On
RewriteCond %{REQUEST_FILENAME} !-f
RewriteRule ^ index.html [QSA,L]`;

const webConfigContent = `<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <system.webServer>
    <rewrite>
      <rules>
        <rule name="Handle History Mode and hash fallback" stopProcessing="true">
          <match url="(.*)" />
          <conditions logicalGrouping="MatchAll">
            <add input="{REQUEST_FILENAME}" matchType="IsFile" negate="true" />
            <add input="{REQUEST_FILENAME}" matchType="IsDirectory" negate="true" />
          </conditions>
          <action type="Rewrite" url="/" />
        </rule>
      </rules>
    </rewrite>
  </system.webServer>
</configuration>`;

// Ensure dist directory exists
const distDir = path.join(__dirname, 'dist');
if (!fs.existsSync(distDir)) {
  console.log('❌ Dist directory not found. Please run "npm run build" first.');
  process.exit(1);
}

// Write .htaccess
fs.writeFileSync(path.join(distDir, '.htaccess'), htaccessContent);
console.log('✅ Created .htaccess for Apache servers');

// Write web.config
fs.writeFileSync(path.join(distDir, 'web.config'), webConfigContent);
console.log('✅ Created web.config for IIS servers');

console.log('🎉 Deployment files ready!');
console.log('');
console.log('📋 Next steps:');
console.log('1. Upload the entire "dist" folder to your server');
console.log('2. Ensure your server is configured to serve index.html for all routes');
console.log('3. Test direct URL access: https://thiyen.vn/products/2');
