package org.glycoinfo.batch.wurcs.subsumption.topology.app;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.batch.glyconvert.ConvertSelectSparql;
import org.glycoinfo.batch.glyconvert.GlyConvertSparql;
import org.glycoinfo.convert.GlyConvert;
import org.glycoinfo.rdf.SelectSparqlBean;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;

/**
 * 
 * A class used to retrieve the glycan sequences that do not have a Topology.
 * 
 * @author aoki
 *
 */
public class TopologyConvertSelectSparql extends SelectSparqlBean implements GlyConvertSparql, InitializingBean {
  public static final String SaccharideURI = Saccharide.URI;
  public static final String Sequence = "Sequence";
  public static final String GlycanSequenceURI = "GlycanSequenceURI";
  public static final String AccessionNumber = Saccharide.PrimaryId;
  protected Log logger = LogFactory.getLog(getClass());

  @Autowired(required = true)
  @Qualifier("org.glycoinfo.batch.glyconvert")
  GlyConvert glyConvert;

  String glycanUri;

  public TopologyConvertSelectSparql() {
    super();
    this.prefix = "PREFIX glycan: <http://purl.jp/bio/12/glyco/glycan#>\n"
        + "PREFIX glytoucan:  <http://www.glytoucan.org/glyco/owl/glytoucan#>\n"
        + "Prefix rocs: <http://www.glycoinfo.org/glyco/owl/relation#>\n";
    this.select = "DISTINCT ?" + SaccharideURI + " ?" + AccessionNumber + " ?" + Sequence;
    this.from = "FROM <http://rdf.glytoucan.org/core>\n" + "FROM <http://rdf.glytoucan.org/sequence/wurcs>\n"
        + "FROM <http://rdf.glytoucan.org/topology>";
  }

  public String getFormat() {
    String format = getGlyConvert().getFromFormat();
    return "glycan:carbohydrate_format_" + format;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.glycoinfo.rdf.SelectSparqlBean#getWhere()
   */
  public String getWhere() {
    String where = "?" + SaccharideURI + " a glycan:saccharide .\n" + "?" + SaccharideURI
        + " glytoucan:has_primary_id ?" + AccessionNumber + " .\n" + "?" + SaccharideURI + " glycan:has_glycosequence ?"
        + GlycanSequenceURI + " .\n" + "?" + GlycanSequenceURI + " glycan:has_sequence ?Sequence .\n" + "?"
        + GlycanSequenceURI + " glycan:in_carbohydrate_format " + getFormat() + "\n";
    if (StringUtils.isNotBlank(getSparqlEntity().getValue(GlyConvertSparql.DoNotFilter)))
      return where;
    else
      return where + getFilter();
  }

  /**
   * 
   * The filter removes any non topology-related sequences.
   * 
   * @return
   */
  public String getFilter() {
    return "FILTER NOT EXISTS {\n" + "?" + Saccharide.URI + " rocs:has_topology ?existingseq .\n" + "}";
  }

  @Override
  public GlyConvert getGlyConvert() {
    return glyConvert;
  }
}
