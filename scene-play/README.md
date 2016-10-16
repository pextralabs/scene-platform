Play + Drools
-------------

Sample application that shows how to integrate a Play Framework (Java) application with Drools.

Code:

    app/controllers/Application.java   HTTP request handlers
    app/plugins/Drools.java            A singleton that sets up the Drools Session
    app/drools/Message.java            A Value Object that is sent to the rules engine

    conf/routes                        Defines a / HTTP handler pointing to app.controllers.Application.index()
    conf/META-INF/kmodule.xml          Config file for Drools that sets up a knowledge base pointing to the drools package
    conf/drools/HelloWorld.drl         A simple rule definition
    
    build.sbt                          The build configuration that specifies dependencies on Drools

Run Locally:

1. Get this repo (e.g. `git clone https://github.com/jamesward/play-drools.git`)
2. Run: `./activator ~run`
3. Try it: [http://localhost:9000](http://localhost:9000)
4. The console should show a message was received and processed:

        goodbye, world
        hello, world

Run on Heroku:

1. Get this repo (e.g. `git clone https://github.com/jamesward/play-drools.git`)
2. Create a new Heroku app: `heroku create`
3. Deploy: `git push heroku master`
4. Try it: `heroku open`
5. Check the logs: `heroku logs`
6. The logs should show a message was received and processed:

        2015-12-10T19:47:34.597939+00:00 app[web.1]: goodbye, world
        2015-12-10T19:47:34.594001+00:00 app[web.1]: hello, world