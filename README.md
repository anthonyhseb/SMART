SMART
=====

SMART is an experimental semantic web application that automatically invokes multiple web services to answer the user's query.

There are only four services currently supported, two of which are test services in the "test-services" sub-project.


To install SMART :
  - go to the SMART folder and run the 'mvn package' command.
  - deploy 'SMART/smart-web/target/smart-web.(version).war' to a servlet container of your choice.
  - [optional] deploy the test-services (hosted locally)

Requirements:
  - Apache Maven (preferably a recent version)
  - Java 1.7

Some documentation can be found [here] (http://airccse.org/journal/ijwest/papers/4413ijwest06.pdf).
