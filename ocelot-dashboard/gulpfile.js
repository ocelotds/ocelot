(function () {
	'use strict';
	var gulp = require('gulp'),
			  del = require('del'),
			  fs = require('graceful-fs'),
			  util = require('gulp-util'),
			  size = require('gulp-filesize'),
			  watch = require('gulp-watch'),
			  uglify = require('gulp-uglify'),
			  concat = require('gulp-concat'),
           plumber = require('gulp-plumber'),
			  jshint = require('gulp-jshint'),
			  clean_css = require('gulp-clean-css'),
			  ngAnnotate = require('gulp-ng-annotate'),
			  gconfig = require('./gconfig.js');

	gulp.task('default', ['build']);
	gulp.task('build', ['init', 'css-all', 'js-all', 'clean', 'relocate']);
	gulp.task('css-all', ['init', 'css']);
	gulp.task('js-all', ['init', 'js']);
	gulp.task('w', ['init', 'watch']);

	/****************************************************************************************************************
	 * Task List                                                                                                    *
	 * ------------------------------------------------------------------------------------------------------------ *
	 * Clean        : Clean dist directory to have a safe build                                                     *
	 * Relocate     : Move files (html, css, js, img, ...) to dist/                                                 *
	 * Css          : Minify (in prod only), concat and move to dist/                                               *
	 * Js           : Annotate (angular), JSHint, Uglify (in prod only), concat (+show size) and move to dist/      *
	 * watch        : watch application files                                                                       *
	 * Js-vendors   : Concat and show size of the vendors and move to dist/                                         *
	 ****************************************************************************************************************/

	gulp.task('watch', function () {
		gulp.watch([gconfig.rootApp + '/**/*', "!" + gconfig.rootApp + '/**/*.css', "!" + gconfig.rootApp + '/**/*.js'], ['init', 'build']); // When you change the HTML
		gulp.watch(gconfig.rootApp + '/**/*.js', ['init', 'js']); // When you change the JS
		gulp.watch(gconfig.rootApp + '/**/*.css', ['init', 'css']); // When you change the CSS
		gulp.watch('gulpfile.js', ['build']); // When you change the gulpfile
	});

	gulp.task('clean', function () {
		return del.sync('./' + gconfig.dist);
	});

	gulp.task('relocate', function () {
		gulp.src([gconfig.rootApp + '/**/*', "!" + gconfig.rootApp + '/**/*.css', "!" + gconfig.rootApp + '/**/*.js'], {base: gconfig.rootApp}).pipe(plumber()).pipe(gulp.dest(gconfig.dist));
		gconfig.vendorsAssetFiles.forEach(function (assetFile, index, array) {
			if (!assetFile.source) {
				gulp.src(vendorsFolder + "/" + assetFile).pipe(gulp.dest(gconfig.dist));
			} else {
				gulp.src(vendorsFolder + "/" + assetFile.source).pipe(gulp.dest(gconfig.dist + "/" + assetFile.target));
			}
		});
	});

	gulp.task('css', function () {
		var cssFiles = [];
		// add vendors directory
		gconfig.vendorsCssFiles.forEach(function (vendorCssFile) {
			cssFiles.push(vendorsFolder + "/" + vendorCssFile); // css from vendors
		});
		cssFiles.push(gconfig.rootApp + "/**/*.css"); // css from app
		gulp.src(cssFiles)
				  .pipe(util.env.dev ? util.noop() : clean_css())
				  .pipe(concat(gconfig.cssName))
				  .pipe(gulp.dest(gconfig.dist));
	});

	gulp.task('js', function () {
		gulp.src('gulpfile.js')
				  .pipe(jshint())
				  .pipe(jshint.reporter('jshint-stylish'));

		gulp.src(gconfig.rootApp + '/**/*.js')
				  .pipe(ngAnnotate())
				  .pipe(jshint())
				  .pipe(jshint.reporter('jshint-stylish'))
				  .pipe(util.env.dev ? util.noop() : uglify())
				  .pipe(concat(gconfig.jsName))
				  .pipe(size())
				  .pipe(gulp.dest(gconfig.dist)); // js from app

		var jsFiles = [];
		gconfig.vendorsJsFiles.forEach(function (vendorJsFile) {
			jsFiles.push(vendorsFolder + "/" + vendorJsFile);
		});
		gulp.src(jsFiles)
				  .pipe(util.env.dev ? util.noop() : uglify())
				  .pipe(concat(gconfig.bowerJs))
				  .pipe(size())
				  .pipe(gulp.dest(gconfig.dist)); // js from vendors
	});
	var vendorsFolder = "bower_components";
	gulp.task("init", function () {
		if (fs.existsSync(".bowerrc")) {
			var bowerrc = JSON.parse(fs.readFileSync(".bowerrc", "utf-8"));
			if (bowerrc.directory) {
				vendorsFolder = bowerrc.directory;
			}
		}
		gconfig.dist = util.env.dist || gconfig.dist;
		gconfig.rootApp = util.env.rootapp || gconfig.rootApp;

		gconfig.jsName = util.env.jsname || gconfig.jsName;
		gconfig.cssName = util.env.cssname || gconfig.cssName;
		gconfig.bowerJs = util.env.bowerjs || gconfig.bowerJs;
		console.log(" - target directory:", gconfig.dist);
		console.log(" - vendors libs directory:", vendorsFolder);
		console.log(" - vendors result js file:", gconfig.bowerJs);
		console.log(" - application source directory:", gconfig.rootApp);
		console.log(" - application main js file:", gconfig.jsName);
		console.log(" - application main css file:", gconfig.cssName);
	});
})();
