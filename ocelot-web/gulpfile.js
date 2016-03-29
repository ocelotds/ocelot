(function () {
	'use strict';
	var gulp = require('gulp'),
			  concat = require('gulp-concat'),
			  size = require('gulp-filesize'),
			  jshint = require('gulp-jshint'),
			  uglify = require('gulp-uglify');
	gulp.task('default', ['minify']);
	gulp.task('minify', function () {
		gulp.src('target/classes/ocelot-core.js')
				  .pipe(jshint())
				  .pipe(uglify())
				  .pipe(concat("ocelot-core-min.js"))
				  .pipe(size())
				  .pipe(gulp.dest('target/classes/'));
	});
})();
