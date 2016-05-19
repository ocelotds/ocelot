(function () {
	'use strict';
	var gulp = require('gulp'),
           del = require('del'),
			  concat = require('gulp-concat'),
			  size = require('gulp-filesize'),
			  jshint = require('gulp-jshint'),
			  uglify = require('gulp-uglify');
	gulp.task('default', ['clean', 'minify', 'add-ocelot-services', 'jshint']);
	gulp.task('clean', function () {
		return del.sync('./target/classes/ocelot-core*.js');
	});
	gulp.task('jshint', ['minify', 'add-ocelot-services'], function () {
		return gulp.src(['./target/classes/org/ocelotds/OcelotServices.js', './src/main/resources/ocelot-core.js', './target/classes/ocelot-core*.js'])
				  .pipe(size())
				  .pipe(jshint())
				  .pipe(jshint.reporter('jshint-stylish'));
	});
	gulp.task('minify', ['clean'], function () {
		return gulp.src('./src/main/resources/ocelot-core.js')
				  .pipe(uglify())
				  .pipe(concat('ocelot-core-min.js'))
				  .pipe(gulp.dest('./target/classes/'));
	});
	gulp.task('add-ocelot-services', ['minify'], function () {
				 gulp.src(['./target/classes/org/ocelotds/OcelotServices.js', './src/main/resources/ocelot-core.js'])
				  .pipe(concat('ocelot-core.js'))
				  .pipe(gulp.dest('./target/classes/'));
		return gulp.src(['./target/classes/org/ocelotds/OcelotServices.js', './target/classes/ocelot-core-min.js'])
				  .pipe(concat('ocelot-core-min.js'))
				  .pipe(gulp.dest('./target/classes/'));
	});
})();
