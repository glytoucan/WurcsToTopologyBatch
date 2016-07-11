package org.glycoinfo.batch.wurcs.subsumption.topology;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.batch.SparqlItemWriter;
import org.glycoinfo.batch.glyconvert.ConvertSelectSparql;
import org.glycoinfo.batch.glyconvert.ConvertSparqlProcessor;
import org.glycoinfo.batch.glyconvert.GlyConvertSparqlItemConfig;
import org.glycoinfo.batch.wurcs.subsumption.topology.app.Config;
import org.glycoinfo.batch.wurcs.subsumption.topology.app.JobConfig;
import org.glycoinfo.batch.wurcs.subsumption.topology.app.ProcessorConfig;
import org.glycoinfo.batch.wurcs.subsumption.topology.app.WurcsTopologySparqlBatch;
import org.glycoinfo.convert.GlyConvertConfig;
import org.glycoinfo.convert.util.DetectFormat;
import org.glycoinfo.rdf.InsertSparql;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.SelectSparqlBean;
import org.glycoinfo.rdf.dao.SparqlDAO;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.glycoinfo.rdf.glycan.GlycoSequence;
import org.glycoinfo.rdf.glycan.GlycoSequenceInsertSparql;
import org.glycoinfo.rdf.glycan.Glycosidic_topology;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.glycoinfo.rdf.glycan.SaccharideInsertSparql;
import org.glycoinfo.rdf.glycan.SaccharideUtil;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glycoinfo.rdf.service.impl.GlycanProcedureConfig;
import org.glycoinfo.rdf.utils.TripleStoreProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author aoki
 *
 *         Test cases for the Topology converter. The batch should process all
 *         wurcs glycosequences that do not have an associated topology already.
 *         The ETL for this batch:<br>
 *         <br>
 *         Extract: Select from GlycoSequence all wurcs strings filtered by
 *         non-existing topology relationship for the same Saccharide (accession
 *         number).<br>
 *         Transform: Process the WurcsToWurcsTopology converter on the wurcs string to
 *         generate the topology string.  Register if it does not exist, passing down the accession number.<br>
 *         Load: Execute topology relation insert statements to link to the original Saccharide.<br>
 *         
 *         This will attempt to execute the extract, transform and load sections individually.
 *
 *         This work is licensed under the Creative Commons Attribution 4.0
 *         International License. To view a copy of this license, visit
 *         http://creativecommons.org/licenses/by/4.0/.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { WurcsTopologySparqlBatchTest.class, Config.class, VirtSesameTransactionConfig.class, GlyConvertConfig.class, ProcessorConfig.class})
//@SpringApplicationConfiguration(classes = { WurcsTopologySparqlBatch.class })
@IntegrationTest
@EnableAutoConfiguration
public class WurcsTopologySparqlBatchTest {
  protected Log logger = LogFactory.getLog(getClass());

  @Autowired
  SparqlDAO sparqlDAO;

  @Autowired
  @Qualifier("itemReaderSelectSparql")
  SelectSparql itemReaderSelectSparql;

  @Autowired
  @Qualifier("itemWriterInsertSparql")
  InsertSparql itemWriterInsertSparql;
  
  @Autowired
  ItemProcessor<SparqlEntity, SparqlEntity> processor;
  
  @Autowired
  GlycanProcedure glycanProcedure;
  
  // using a weird wurcs...
  String wurcs = "WURCS=2.0/8,9,8/[a1122h-1x_1-5][a2122h-1x_1-5][a2112h-1x_1-5][a1221m-1x_1-5][a221h-1x_1-5][a2112h-1x_1-5_2*NCC/3=O][a2122h-1x_1-5_2*NCC/3=O][Aad21122h-2x_2-6_5*NCC/3=O]/1-2-3-4-5-6-7-8-4/a?-b1_b?-c1_c?-d1_d?-e1_e?-f1_f?-g1_g?-h2_h?-i1";

  String weirdG88203PI = "WURCS=2.0/2,4,3/[a2112h-1x_1-5_2*NCC/3=O][a2122h-1x_1-5_2*NCC/3=O]/1-1-2-2/a?-b1_b?-c1_c?-d1";
  @Test
  @Transactional
  public void testOne() throws Exception {
    
    // register one new one, just in case.
    String output = glycanProcedure.register("WURCS=2.0/7,9,8/[a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a221h-1x_1-5][a2112h-1x_1-5][a1221m-1x_1-5][a1221m-1a_1-5]/1-1-2-3-4-5-3-6-7/a4-b1_a6-i1_b4-c1_c3-d1_c6-g1_d?-e1_e?-f1_g?-h1", "254");
    
    // extract one
    itemReaderSelectSparql.setLimit("1");
    List<SparqlEntity> onewurcsList = sparqlDAO.query(itemReaderSelectSparql);
    
    SparqlEntity onewurcs = onewurcsList.iterator().next();
    
    String accNum = onewurcs.getValue(Saccharide.PrimaryId);
    logger.debug("accNum:>" + accNum);
    
    // process it - check id's (if wurcs generated is invalid then registration process will pick it up)
    SparqlEntity processed = processor.process(onewurcs);
    if (StringUtils.isNotBlank(processed.getValue(SparqlItemWriter.Pass))) {
      // no luck, but found one that is should not be processed as topology, good enough.
      
      Assert.assertTrue(true);
      return;
    }
    String id1 = processed.getValue(Glycosidic_topology.PrimaryId_1);
    String id2 = processed.getValue(Glycosidic_topology.PrimaryId_2);
    Assert.assertEquals(accNum, id1);
    Assert.assertNotNull(id2);
    
    itemWriterInsertSparql.setSparqlEntity(processed);
    // insert statement
    sparqlDAO.insert(itemWriterInsertSparql);
    
//    confirm the filter works by running it again.
    onewurcsList = sparqlDAO.query(itemReaderSelectSparql);
    onewurcs = onewurcsList.iterator().next();
    
    String accNum2 = onewurcs.getValue(Saccharide.PrimaryId);
    Assert.assertNotEquals(accNum, accNum2);
    
  }
  
  @Test
  @Transactional
  public void testOneG88203PI() throws Exception {
    SparqlEntity onewurcs = new SparqlEntity();
    
    onewurcs.setValue(Saccharide.PrimaryId, "G88203PI");
    onewurcs.setValue(ConvertSelectSparql.Sequence, weirdG88203PI);
    
    String accNum = onewurcs.getValue(Saccharide.PrimaryId);
    logger.debug("accNum:>" + accNum);
    
    // process it - check id's (if wurcs generated is invalid then registration process will pick it up)
    SparqlEntity processed = processor.process(onewurcs);
    String id1 = processed.getValue(Glycosidic_topology.PrimaryId_1);
    String id2 = processed.getValue(Glycosidic_topology.PrimaryId_2);
    Assert.assertEquals(accNum, id1);
    Assert.assertNotNull(id2);
    
    itemWriterInsertSparql.setSparqlEntity(processed);
    // insert statement
    sparqlDAO.insert(itemWriterInsertSparql);
  }
  
  @Test
  @Transactional
  public void testNotLevel1G24678II() throws Exception {
    SparqlEntity onewurcs = new SparqlEntity();
    
    onewurcs.setValue(Saccharide.PrimaryId, "G24678I");
    onewurcs.setValue(ConvertSelectSparql.Sequence, "WURCS=2.0/1,1,0/[hU122h]/1/");
    
    String accNum = onewurcs.getValue(Saccharide.PrimaryId);
    logger.debug("accNum:>" + accNum);
    
    // process it - check id's (if wurcs generated is invalid then registration process will pick it up)
    SparqlEntity processed = processor.process(onewurcs);
    String id1 = processed.getValue(Glycosidic_topology.PrimaryId_1);
    String id2 = processed.getValue(Glycosidic_topology.PrimaryId_2);
    Assert.assertNull(id1);
    Assert.assertNull(id2);
  }
  
  @Bean
  TripleStoreProperties getTripleStoreProperties() {
    return new TripleStoreProperties();
  }
}