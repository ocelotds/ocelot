CURRENT_VERSION=`python -c "import xml.etree.ElementTree as ET; print(ET.parse(open('pom.xml')).getroot().find('{http://maven.apache.org/POM/4.0.0}version').text)"`
echo "CURRENT_VERSION = "$CURRENT_VERSION
echo "releaseVersion = "$releaseVersion
env:
  - PERFORM=false
PERFORM=false
if [ "$CURRENT_VERSION" = "$releaseVersion-SNAPSHOT" ]; then
env:
  - PERFORM=true
fi
echo "PERFORM = "$PERFORM
TAG_NAME=`python -c "import xml.etree.ElementTree as ET; print(ET.parse(open('pom.xml')).getroot().find('{http://maven.apache.org/POM/4.0.0}artifactId').text)"`-$releaseVersion
echo "TAG_NAME = "$TAG_NAME
# mvn --B -Dtag=$PROJECT_NAME-$releaseVersion release:prepare \
#                 -DreleaseVersion=$releaseVersion \
#                 -DdevelopmentVersion=$developmentVersion
