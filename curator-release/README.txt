1. Run prepare-curator-release.sh in the root directory

2. Run 'ant dist' in both the verb and nom directories here.

3. Copy all the jars from the lib directory into the lib directory
   within $CURATOR_HOME/dist.

4. Copy illinois-verb-srl/dist/illinois-verb-srl-server.jar and
   illinois-nom-srl/dist/illinois-nom-srl-server.jar into
   $CURATOR_HOME/dist/components.

5. Copy config/srl-config.properties into
   $CURATOR_HOME/dist/configs.

6. Copy illinois-verb-srl/illinois-verb-srl-server.sh and
   illinois-nom-srl/illinois-nom-srl-server.sh to the bin directory
   within $CURATOR_HOME/dist/bin


Now the SRL servers are ready to be launched.
