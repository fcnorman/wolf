data.aggregator.batch
=====================

This is the wolf data.aggregator.batch module.

To install:
```
PROJECT_DIR=<your project dir>
cd $PROJECT_DIR
cd wolf
cd data.aggregator.batch
cd bin

nano install.sh and make sure you are Ok with it.

./install.sh and let the installation run.  Watch for any errors.
```

To configure:
```
Copy run.sh.template to run.sh and configure your settings.

nano run.sh and make sure you are Ok with it.  Edit PROJECT_DIR so that it points to
the directory which contains your wolf sub-directory.  Change the localhosts and 127.0.0.1
to correct hostnames.
```

To run:
```
chmod +x run.sh

./run.sh and let it run.  Check ps -ef to see the processes running.  Check log files
for errors.
```

To be helpful:
```
Fork the github repository to your repository.
Clone your repository to your development machine.
Use git to manage your changes.
When you have something good to contribute back, create a pull request.  (See github for instructions.)

