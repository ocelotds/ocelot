if [ "$TRAVIS_BRANCH " = "release" ]; then
git config --global user.email "ocelotds.francois@gmail.com"
git config --global user.name "Travis-CI"
mvn --B release:clean release:prepare release:perform --settings .travis-settings.xml
else
mvn deploy --settings .travis-settings.xml
fi
