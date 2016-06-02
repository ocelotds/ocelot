angular.module('app.dashboard').filter('sessioninfo', function () {
	return function (session) {
		if(session) {
			return session.username!=='ANONYMOUS'?session.username:session.id;
		}
		return null;
	};
});

