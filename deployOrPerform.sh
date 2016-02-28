CURRENT_VERSION=`python -c "import xml.etree.ElementTree as ET; print(ET.parse(open('pom.xml')).getroot().find('{http://maven.apache.org/POM/4.0.0}version').text)"`
tag=`python -c "import xml.etree.ElementTree as ET; print(ET.parse(open('pom.xml')).getroot().find('{http://maven.apache.org/POM/4.0.0}artifactId').text)"`-$releaseVersion
echo "CURRENT_VERSION = "$CURRENT_VERSION
echo "tag = "$tag
echo "releaseVersion = "$releaseVersion
echo "developmentVersion = "$developmentVersion
if [ "$CURRENT_VERSION" = "$releaseVersion-SNAPSHOT" ]; then
echo "PERFORM "$releaseVersion
mvn --B release:prepare -Dtag=$tag-$releaseVersion -DreleaseVersion=$releaseVersion -DdevelopmentVersion=$developmentVersion
else
echo "DEPLOY "$CURRENT_VERSION
mvn deploy --settings .travis-settings.xml
fi
