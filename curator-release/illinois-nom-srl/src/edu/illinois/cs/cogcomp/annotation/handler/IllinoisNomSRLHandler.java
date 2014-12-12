package edu.illinois.cs.cogcomp.annotation.handler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.Forest;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.thrift.parser.Parser;

import edu.illinois.cs.cogcomp.srl.SemanticRoleLabeler;

/**
 * Wraps the Illinois nom SRL in a Parser.Iface.
 * @author Mark Sammons
 *
 */

public class IllinoisNomSRLHandler extends IllinoisAbstractHandler implements Parser.Iface {
	
	
    protected static final String DEFAULT_CONFIG = "configs/srl-config.properties";
    private final Logger logger = LoggerFactory.getLogger(IllinoisNomSRLHandler.class);

    private SemanticRoleLabeler srlSystem;

    public IllinoisNomSRLHandler() {

        this( DEFAULT_CONFIG );
    }
	
    public IllinoisNomSRLHandler(String configFileName) {
        super("Illinois Nom Semantic Role Labeler" );

        logger.info("Nom SRL ready");
        if (configFileName.trim().equals("")) {
            configFileName = DEFAULT_CONFIG;
        }

        // initialize the system

	try  {
	    this.srlSystem = new SemanticRoleLabeler(configFileName, "Nom");
	} catch(Exception e) {
	    logger.error("Error initializing Nom SRL", e);
	    throw new RuntimeException(e);
	}

        super.setVersion( srlSystem.getVersion() );
        super.setName( srlSystem.getSRLCuratorName() );
        super.setIdentifier( srlSystem.getSRLCuratorName() );
                
        logger.info("set name to '" + srlSystem.getSRLCuratorName() + "'." );
        logger.info("set version to '" + srlSystem.getVersion() + "'." );

    }

    @Override
    public Forest parseRecord(Record record) throws AnnotationFailedException,
                                                    TException {
	try {
	    return srlSystem.getSRLForest(record);
	} catch(Exception e) {
	    logger.error("Error annotating record", e);
	    throw new AnnotationFailedException(e.getMessage());
	}
    }


}
