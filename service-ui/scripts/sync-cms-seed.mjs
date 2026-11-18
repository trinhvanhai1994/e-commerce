// Copies the CMS page seed from service-api into the frontend so the packaged defaults never drift.
// Runs before `dev` and `build`. When service-api is not next to this folder (e.g. a Docker build with only
// service-ui in context), the committed copy is used as is.
import { copyFileSync, existsSync, readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const pairs = [
  [
    resolve(root, '../service-api/src/main/resources/cms-seed/page-sections.json'),
    resolve(root, 'src/content/page-sections.seed.json'),
  ],
]

for (const [source, target] of pairs) {
  if (!existsSync(source)) {
    console.log(`[sync-cms-seed] ${source} not found, keeping ${target}`)
    continue
  }
  if (existsSync(target) && readFileSync(source, 'utf8') === readFileSync(target, 'utf8')) continue
  copyFileSync(source, target)
  console.log(`[sync-cms-seed] updated ${target}`)
}
