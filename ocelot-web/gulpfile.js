(function () {
	'use strict';
	var gulp = require('gulp'),
			  concat = require('gulp-concat'),
			  size = require('gulp-filesize'),
			  jshint = require('gulp-jshint'),
			  uglify = require('gulp-uglify');
	gulp.task('default', ['minify']);
	gulp.task('minify', function () {
		gulp.src(['target/classes/ocelot-cache.js', 'target/classes/ocelot-constants.js',
		'target/classes/ocelot-controller.js', 'target/classes/ocelot-core.js',
		'target/classes/ocelot-promises.js', 'target/classes/md5-tools.js'])
				  .pipe(size())
				  .pipe(jshint())
				  .pipe(jshint.reporter('jshint-stylish'))
//				  .pipe(uglify())
				  .pipe(concat("ocelot-core-min.js"))
				  .pipe(gulp.dest('target/classes/'));
		gulp.src('target/classes/ocelot-core-min.js')
				  .pipe(size());
	});
})();
