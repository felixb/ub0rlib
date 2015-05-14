ub0rlib
=======

This is a library for all my projects [1].
Feel free to use it for your own projects.

Currently this lib contains:
 * Wrapper for Contacts API
 * Wrapper for Telephony API
 * Some utils

Including in your project
=========================

Either include it in your project as an android library project, or grab it via maven:

    <dependency>
        <groupId>de.ub0r.android.lib</groupId>
        <artifactId>lib</artifactId>
        <version>{latest.version}</version>
        <type>jar</type>
    </dependency>

Or add it in your build.gradle

    repositories {
        maven {
            url 'https://raw.githubusercontent.com/felixb/mvn-repo/master/'
        }
        mavenCentral()
    }

    dependencies {
        compile 'de.ub0r.android.lib:lib:+'
    }

License
=======

    Copyright 2009 - 2015 Felix Bechstein

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[1] http://github.com/felixb/
