/**
 * 
 * Default value, can be override by gulp arguments
 * example : gulp --rootapp=app --dist=dist --bowerfolder=bower_components --jsname=appscripts.js --cssname=styles.css --bowerjs=bower.js
 */
/**
 * The directory where gulp export 
 */
module.exports.dist = 'target/classes/js';
/**
 * The source root of website
 */
module.exports.rootApp = 'src/main/resources/js';
/**
 * The name of js file result from app js
 */
module.exports.jsName = 'main.js'; 
/**
 * The name of css file result from app css and bower css
 */
module.exports.cssName = 'main.css'; 
/**
 * The name of js file result from bower js
 */
module.exports.bowerJs = 'vendors.js';
/**
 * All vendors css files, they will be concat in cssName file
 */
module.exports.vendorsCssFiles = [
];
/**
 * All dependencies js files from bower install
 */
module.exports.vendorsJsFiles = [
];
/**
 * Asset vendors files, can be a simple resource or structured object {"source":"", "target":""}
 */
module.exports.vendorsAssetFiles = [
];

