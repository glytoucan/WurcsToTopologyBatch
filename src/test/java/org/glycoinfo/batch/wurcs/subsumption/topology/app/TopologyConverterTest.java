package org.glycoinfo.batch.wurcs.subsumption.topology.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.batch.glyconvert.ConvertInsertSparql;
import org.glycoinfo.batch.glyconvert.GlyConvertSparqlItemConfig;
import org.glycoinfo.batch.wurcs.subsumption.topology.app.Config;
import org.glycoinfo.batch.wurcs.subsumption.topology.app.TopologyConverter;
import org.glycoinfo.convert.GlyConvertConfig;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.glycoinfo.rdf.glycan.GlycoSequence;
import org.glycoinfo.rdf.glycan.Glycosidic_topology;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.glycoinfo.rdf.service.impl.GlycanProcedureConfig;
import org.glycoinfo.rdf.utils.SparqlEntityConverter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { VirtSesameTransactionConfig.class, GlycanProcedureConfig.class, TopologyConverterTest.class, GlyConvertConfig.class })
@Configuration
@EnableAutoConfiguration
@IntegrationTest
public class TopologyConverterTest {

  private static final Log logger = LogFactory.getLog(TopologyConverterTest.class);
  
  @Autowired
  SparqlEntityConverter<SparqlEntity> converter;

  @Bean
  SparqlEntityConverter<SparqlEntity> converter() {
    return new TopologyConverter();
  }

  @Test(expected=SparqlException.class)
  @Transactional
  public void testInvalid() throws SparqlException {
    SparqlEntity e = new SparqlEntity();
    e.setValue(Saccharide.PrimaryId, "testid");
    e.setValue(GlycoSequence.Sequence, "testsequence");
    e.setValue(ConvertInsertSparql.ConvertedSequence, "convertedtestsequence");
    converter.convert(e);
  }
  
//  @Test
//  @Transactional
//  public void testG00055MO() throws SparqlException {
//    SparqlEntity e = new SparqlEntity();
//    e.setValue(Saccharide.PrimaryId, "G00055MO");
//    e.setValue(GlycoSequence.Sequence, "WURCS=2.0/2,2,1/[a2122h-1b_1-5_2*NCC/3=O][a2112h-1b_1-5]/1-2/a4-b1");
//    // TODO get the proper topology of this structure, but for now confirm the id's are there.
//    e.setValue(ConvertInsertSparql.ConvertedSequence, "WURCS=2.0/2,2,1/[a2122h-1b_1-5_2*NCC/3=O][a2112h-1b_1-5]/1-2/a4-b1");
//    SparqlEntity result = converter.convert(e);
//
//    logger.debug(result);
//    // original accession number => PrimaryId_1
//    Assert.assertNotNull(result.getValue(Glycosidic_topology.PrimaryId_1));
//
//    // topology acc num => PrimaryId_2
//    Assert.assertNotNull(result.getValue(Glycosidic_topology.PrimaryId_2));
//  }
}
