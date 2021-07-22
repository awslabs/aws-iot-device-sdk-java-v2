# AWS IoT Device SDK for Java v2

This document provides information about the AWS IoT device SDK for Java V2.

If you have any issues or feature requests, please file an issue or pull request.

This SDK is built on the AWS Common Runtime, a collection of libraries
([aws-c-common](https://github.com/awslabs/aws-c-common),
[aws-c-io](https://github.com/awslabs/aws-c-io),
[aws-c-mqtt](https://github.com/awslabs/aws-c-mqtt),
[aws-c-http](https://github.com/awslabs/aws-c-http),
[aws-c-cal](https://github.com/awslabs/aws-c-cal),
[aws-c-auth](https://github.com/awslabs/aws-c-auth),
[s2n](https://github.com/awslabs/s2n)...) written in C to be
cross-platform, high-performance, secure, and reliable. The libraries are bound
to Java by the [aws-crt-java](https://github.com/awslabs/aws-crt-java) package.

*__Jump To:__*

* [Installation](#Installation)
* [Mac-Only TLS Behavior](#Mac-Only-TLS-Behavior)
* [Samples](samples)
* [Getting Help](#Getting-Help)
* [Giving Feedback and Contributions](#Giving-Feedback-and-Contributions)

## Installation

### Minimum Requirements

* Java 8 or above

* Set JAVA_HOME first

### Requirements to build the AWS CRT locally

* CMake 3.1+
* Clang 3.9+ or GCC 4.4+ or MSVC 2015+

### Consuming IoT Device SDK from Maven

Consuming this SDK via Maven is the preferred method of consuming it. Add the following to your pom.xml depedencies:

``` xml
<dependency>
  <groupId>software.amazon.awssdk.iotdevicesdk</groupId>
  <artifactId>aws-iot-device-sdk</artifactId>
  <version><!-- latest release version --></version>
</dependency>
```

Look up the latest SDK version here: https://github.com/aws/aws-iot-device-sdk-java-v2/releases

### Build IoT Device SDK from source

``` sh
git clone https://github.com/awslabs/aws-iot-device-sdk-java-v2.git
# update the version of the CRT being used
mvn versions:use-latest-versions -Dincludes="software.amazon.awssdk.crt*"
mvn clean install
```

### Build CRT from source

``` sh
# NOTE: use the latest version of the CRT here


git clone --branch v0.11.5 https://github.com/awslabs/aws-crt-java.git

git clone https://github.com/awslabs/aws-iot-device-sdk-java-v2.git
cd aws-crt-java
mvn install -Dmaven.test.skip=true
cd ../aws-iot-device-sdk-java-v2
mvn clean install
```

#### Android

Supports API 26 or newer.
NOTE: The shadow sample does not currently complete on android due to its dependence on stdin keyboard input.

``` sh
git clone --recursive --branch v0.11.5 https://github.com/awslabs/aws-crt-java.git
git clone https://github.com/awslabs/aws-iot-device-sdk-java-v2.git
cd aws-crt-java/android
./gradlew connectedCheck # optional, will run the unit tests on any connected devices/emulators
./gradlew publishToMavenLocal
cd ../aws-iot-device-sdk-java-v2/android
./gradlew publishToMavenLocal
./gradlew installDebug # optional, will install the IoTSamples app to any connected devices/emulators
```

Add the following to your project's build.gradle:

``` groovy
repositories {
    mavenCentral()
    maven {
        url System.getenv('HOME') + "/.m2/repository"
    }
}

dependencies {
    implementation 'software.amazon.awssdk.crt:android:0.11.5'
}
```
#### Caution
You will need to override and provide a ROOT_CERTIFICATE manually from one of the following [certificates](https://www.amazontrust.com/repository/). For overriding default trust store you can use following [method](https://github.com/aws/aws-iot-device-sdk-java-v2/blob/ed802dce740895bcd3b0b91de30ec49407e34a87/sdk/src/main/java/software/amazon/awssdk/iot/AwsIotMqttConnectionBuilder.java#L151-L160). It's a [known problem](https://github.com/aws/aws-iot-device-sdk-java-v2/issues/157).


## Mac-Only TLS Behavior

Please note that on Mac, once a private key is used with a certificate, that certificate-key pair is imported into the Mac Keychain.  All subsequent uses of that certificate will use the stored private key and ignore anything passed in programmatically.  When a stored private key from the Keychain is used, the following will be logged at the "info" log level:

```
static: certificate has an existing certificate-key pair that was previously imported into the Keychain.  Using key from Keychain instead of the one provided.
```

## Samples

[Samples README](samples)

## Getting Help

The best way to interact with our team is through GitHub. You can [open an issue](https://github.com/aws/aws-iot-device-sdk-java-v2/issues) and choose from one of our templates for guidance, bug reports, or feature requests. You may also find help on community resources such as [StackOverFlow](https://stackoverflow.com/questions/tagged/aws-iot) with the tag #aws-iot or If you have a support plan with [AWS Support](https://aws.amazon.com/premiumsupport/), you can also create a new support case.

Please make sure to check out our resources too before opening an issue:

* Our [Developer Guide](https://docs.aws.amazon.com/iot/latest/developerguide/what-is-aws-iot.html) ([source](https://github.com/awsdocs/aws-iot-docs))
* Check for similar [Issues](https://github.com/aws/aws-iot-device-sdk-java-v2/issues)
* [AWS IoT Core Documentation](https://docs.aws.amazon.com/iot/)
* [Dev Blog](https://aws.amazon.com/blogs/?awsf.blog-master-iot=category-internet-of-things%23amazon-freertos%7Ccategory-internet-of-things%23aws-greengrass%7Ccategory-internet-of-things%23aws-iot-analytics%7Ccategory-internet-of-things%23aws-iot-button%7Ccategory-internet-of-things%23aws-iot-device-defender%7Ccategory-internet-of-things%23aws-iot-device-management%7Ccategory-internet-of-things%23aws-iot-platform)
* Integration with AWS IoT Services such as
[Device Shadow](https://docs.aws.amazon.com/iot/latest/developerguide/iot-device-shadows.html)
[Jobs](https://docs.aws.amazon.com/iot/latest/developerguide/iot-jobs.html)
[Fleet Provisioning](https://docs.aws.amazon.com/iot/latest/developerguide/provision-wo-cert.html)
is provided by code that been generated from a model of the service.

## Giving Feedback and Contributions

We need your help in making this SDK great. Please participate in the community and contribute to this effort by submitting issues, participating in discussion forums and submitting pull requests through the following channels.

* [Contributions Guidelines](CONTRIBUTING.md)
* Articulate your feature request or upvote existing ones on our [Issues](https://github.com/aws/aws-iot-device-sdk-java-v2/issues?q=is%3Aissue+is%3Aopen+label%3Afeature-request) page.
* Submit [Issues](https://github.com/aws/aws-iot-device-sdk-java-v2/issues)

## License

This library is licensed under the Apache 2.0 License.
