package org.glycoinfo.batch.wurcs.subsumption.topology.app;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.batch.SparqlItemConfig;
import org.glycoinfo.batch.SparqlItemWriter;
import org.glycoinfo.batch.glyconvert.ConvertInsertSparql;
import org.glycoinfo.rdf.DuplicateException;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.glycan.GlycoSequence;
import org.glycoinfo.rdf.glycan.Glycosidic_topology;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glycoinfo.rdf.utils.SparqlEntityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * 
 * This is a Post conversion process that accepts the results from the wurcs
 * topology conversion (To Values) and processes the correct values for the
 * wurcstopologyinsert sparqlbean.
 * 
 * @author aoki
 *
 * @param <SparqlEntity>
 */
@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "prototype")
public class TopologyConverter implements SparqlEntityConverter<SparqlEntity> {
  private static final Log logger = LogFactory.getLog(TopologyConverter.class);

  @Autowired
  GlycanProcedure glycanProcedure;

  @Override
  public SparqlEntity convert(SparqlEntity e) throws SparqlException {
    // grab the converted sequence
    // ConvertInsertSparql.ConvertedSequence
    String sequence = e.getValue(ConvertInsertSparql.ConvertedSequence);
    String topAccessionNumber = null;
    // register it => topology acc num
    
    if (null == sequence && StringUtils.isNotBlank(e.getValue(SparqlItemWriter.Pass))) {
      // error occurred, cannot register.
      return null;
    }

    try {
    topAccessionNumber = glycanProcedure.register(sequence, "1");
    } catch (DuplicateException dupe) {
      topAccessionNumber = dupe.getId();
    } catch (SparqlException se) {
      logger.debug("message:>" + se.getMessage());
      logger.debug("cause:>" + se.getCause());
      logger.debug(Saccharide.PrimaryId + ":>" + e.getObjectValue(Saccharide.PrimaryId));
      throw se;
    }

    if (e.getValue(Saccharide.PrimaryId).equals(topAccessionNumber))
      return null;
    
    // original accession number => PrimaryId_1
    e.setValue(Glycosidic_topology.PrimaryId_1, e.getValue(Saccharide.PrimaryId));

    // topology acc num => PrimaryId_2
    e.setValue(Glycosidic_topology.PrimaryId_2, topAccessionNumber);

    return e;
  }
}
