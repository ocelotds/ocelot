(function () {
	'use strict';
	var gulp = require('gulp'),
			  del = require('del'),
			  concat = require('gulp-concat'),
			  size = require('gulp-filesize'),
			  jshint = require('gulp-jshint'),
			  uglify = require('gulp-uglify');
	gulp.task('default', ['clean', 'jshint', 
		'createCore', 'minifyCore', 
		'createAngular', 'minifyAngular'
	]);
	gulp.task('clean', function () {
		return del.sync('./target/classes/js/*');
	});
	gulp.task('jshint', ['clean'], function () {
		return gulp.src('./src/main/resources/js/*')
				  .pipe(size())
				  .pipe(jshint())
				  .pipe(jshint.reporter('jshint-stylish'));
	});
	gulp.task('createAngular', ['clean'], function () {
			return gulp.src([ 
				'./src/main/resources/js/ng/ocelot.core.js',
				'./src/main/resources/js/ng/promiseFactory.js',
				'./src/main/resources/js/ng/ocelotServices.js',
				'./src/main/resources/js/ng/hash.js', 
				'./src/main/resources/js/ng/subscriberFactory.js'
			]).pipe(concat('core.ng.js')).pipe(gulp.dest('./target/classes/js'));
	});
	gulp.task('createCore', ['clean'], function () {
			return gulp.src([
				'./src/main/resources/js/nofwk/hash.js',
				'./src/main/resources/js/nofwk/subscriberFactory.js',
				'./src/main/resources/js/nofwk/ocelotServices.js', 
				'./src/main/resources/js/nofwk/promiseFactory.js', 
				'./src/main/resources/js/nofwk/ocelotController.js'
			]).pipe(concat('core.js')).pipe(gulp.dest('./target/classes/js'));
	});
	gulp.task('minifyAngular', ['clean', 'createAngular'], function () {
			return gulp.src('./target/classes/js/core.ng.js')
				  .pipe(uglify())
				  .pipe(concat('core.ng.min.js'))
				  .pipe(gulp.dest('./target/classes/js'));
	});
	gulp.task('minifyCore', ['clean', 'createCore'], function () {
			return gulp.src('./target/classes/js/core.js')
				  .pipe(uglify())
				  .pipe(concat('core.min.js'))
				  .pipe(gulp.dest('./target/classes/js'));
	});
})();
