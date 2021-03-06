# DropWizard Tika Server

This provides a DropWizard wrapper around Apache Tika, allowing documents
of many types to be converted into plain text, wrapped in JSON.


## Building

To build the application, use the following maven command:

    mvn clean package

This will build a jar file containing all of the required dependencies in the
`target` directory.

**Note**: If you are installing to Heroku, the recommendation is to not
store dependencies in the default maven repository. In this case, uncomment
the maven-dependency-plugin block in the pom.xml (under the plugins block).


## Running the application

The application can be run from the command line using the following:

    java -jar target/dropwizard-tika-server-0.1.jar server config/tika.yml

The given config file defines the ports the application will use, logging details,
and authentication options.


## Usage

The application has just one endpoint, with three sub-options:

    http://localhost:8080/tika/metadata
    http://localhost:8080/tika/fulldata
    http://localhost:8080/tika/text

For each of these, the application expects a `PUT` request with the file
passed in the request body. For example, using `curl` from the command line:

    curl -T example.pdf http://localhost:8080/tika/metadata

Each option will return a JSON object with the following values:

```
{
  "status": "OK|ERROR",
  "msg": "Error message, if in error state, otherwise null",
  "metadata": {
    // Metadata here, if using metadata or fulldata options
  },
  "text": "Document text, if using text or fulldata options"
}
```

**Note**: by default, curl will add an "Expect: 100-Continue" header to the 
request which causes problems if running the application behind lighttpd. This
can be disabled by adding your own empty "Expect" header, like so:

	curl -H "Expect:" -T example.pdf http:://localhost:8080/tika/metadata


## Authentication

To enable basic authentication, change the `authentication.enabled` parameter
in the tika.yml configuration file to `true`. Once this is set, you will need to pass
the required username and password as system parameters on the command line, like so:

    java -DAUTH_USERNAME=user -DAUTH_PASSWORD=pass -jar dropwizard-tika-server-0.1.jar server config/tika.yml  
    
The system properties used may be changed if necessary in the config file.


## Credits

This application was based upon gselva's [Simple-Tika-Server](https://github.com/gselva/Simple-Tika-Server),
which unfortunately I was unable to get working as advertised. It seemed 
easier, since it hadn't been worked on for four years, to use it as the basis
for a new application - I wasn't able to debug what was already there in the
time available.
