package org.glycoinfo.batch.wurcs.subsumption.topology.app;

import org.glycoinfo.batch.glyconvert.ConvertSparqlProcessor;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessorConfig {

  @Autowired
  TopologyConverter topologyConverter;
  
  @Autowired
  ConvertSparqlProcessor convertSparqlProcessor;

  @Bean
  public ItemProcessor<SparqlEntity, SparqlEntity> processor() {
    convertSparqlProcessor.setPostConverter(topologyConverter);
    return convertSparqlProcessor;
  }
  
  @Bean
  public ConvertSparqlProcessor convertSparqlProcessor() {
    return new ConvertSparqlProcessor();
  }
  
  @Bean
  public TopologyConverter topologyConverter() {
    return new TopologyConverter();
  }
}