# DropWizard Tika Server

This provides a DropWizard wrapper around Apache Tika, allowing documents
of many types to be converted into plain text, wrapped in JSON.


## Building

To build the application, use the following maven command:

    mvn clean package

This will build a jar file containing all of the required dependencies in the
`target` directory.


## Running the application

The application can be run from the command line using the following:

    java -jar target/dropwizard-tika-server-1.0.jar server config/tika.yml

The given config file defines the ports the application will use. No further
config is required at this time.


## Usage

The application has just one endpoint, with three sub-options:

    http://localhost:8080/tika/metadata
    http://localhost:8080/tika/fulldata
    http://localhost:8080/tika/text

Each option will return a JSON object with the following values:

```
{
  "status": "OK|ERROR",
  "msg": "Error message, if in error state, otherwise null",
  "metadata": {
    // Metadata here, if using metadata or fulldata options
  },
  "text": {
    // Document text, if using text of fulldata options
  }
}
```


## Credits

This application was based upon gselva's [Simple-Tika-Server](https://github.com/gselva/Simple-Tika-Server),
which unfortunately I was unable to get working as advertised. It seemed 
easier, since it hadn't been worked on for four years, to use it as the basis
for a new application - I wasn't able to debug what was already there in the
time available.
