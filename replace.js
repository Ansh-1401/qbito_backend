const fs = require('fs');
const path = require('path');

function replaceInFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf8');
    let original = content;

    content = content.replace(/@CrossOrigin\(origins = "http:\/\/localhost:5173"\)/g, '@CrossOrigin(origins = "${app.frontend.url}")');

    if (content !== original) {
        fs.writeFileSync(filePath, content);
        console.log('Updated ' + filePath);
    }
}

function traverse(dir) {
    if (!fs.existsSync(dir)) return;
    fs.readdirSync(dir).forEach(file => {
        let fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            traverse(fullPath);
        } else if (fullPath.endsWith('.java')) {
            replaceInFile(fullPath);
        }
    });
}

traverse('./src/main/java/com/scan2dine/scan2dine');
