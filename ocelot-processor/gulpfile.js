(function () {
	'use strict';
	var gulp = require('gulp'),
			  del = require('del'),
			  concat = require('gulp-concat'),
			  size = require('gulp-filesize'),
			  jshint = require('gulp-jshint'),
			  uglify = require('gulp-uglify');
	gulp.task('default', ['clean', 'jshint', 'createCore', 'minifyCore', 'copyAngular', 'copyAndMinifyPromiseFactory']);
	gulp.task('clean', function () {
		return del.sync('./target/classes/js/*');
	});
	gulp.task('jshint', ['clean'], function () {
		return gulp.src('./src/main/resources/js/*')
				  .pipe(size())
				  .pipe(jshint())
				  .pipe(jshint.reporter('jshint-stylish'));
	});
	gulp.task('copyAndMinifyPromiseFactory', ['clean'], function () {
			return gulp.src(['./src/main/resources/js/nofwk/promiseFactory.js'])
					  .pipe(uglify())
					  .pipe(gulp.dest('./target/classes/js'));
	});
	gulp.task('copyAngular', ['clean'], function () {
			return gulp.src([
				'./src/main/resources/js/angularjs/hash.js', 
				'./src/main/resources/js/angularjs/ocelot.core.js',
				'./src/main/resources/js/angularjs/ocelot.services.js',
				'./src/main/resources/js/angularjs/promiseFactory.js', 
				'./src/main/resources/js/angularjs/subscriberFactory.js'
			]).pipe(gulp.dest('./target/classes/js/angularjs'));
	});
	gulp.task('createCore', ['clean'], function () {
			return gulp.src([
				'./src/main/resources/js/nofwk/subscriberFactory.js',
				'./src/main/resources/js/nofwk/OcelotServices.js', 
				'./src/main/resources/js/nofwk/promiseFactory.js', 
				'./src/main/resources/js/nofwk/hash.js', 
				'./src/main/resources/js/nofwk/OcelotController.js'
			]).pipe(concat('ocelot-core.js')).pipe(gulp.dest('./target/classes/js'));
	});
	gulp.task('minifyCore', ['clean', 'createCore'], function () {
			return gulp.src('./target/classes/js/ocelot-core.js')
				  .pipe(uglify())
				  .pipe(concat('ocelot-core-min.js'))
				  .pipe(gulp.dest('./target/classes/js'));
	});
	gulp.task('minifyPromise', ['clean'], function () {
			return gulp.src('./src/main/resources/js/promisefactory.js')
				  .pipe(uglify())
				  .pipe(concat('promisefactory.js'))
				  .pipe(gulp.dest('./target/classes/js'));
	});
})();
