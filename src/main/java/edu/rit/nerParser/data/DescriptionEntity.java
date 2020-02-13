package edu.rit.nerParser.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Description Entity:  Entity containing the descriptive text
 * of a vulnerability and the name entity recognition results
 * from analyzing the text.
 *
 * @author wstumbo
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "description", schema = "nist")
public class DescriptionEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Exclude
  private int id;

  /**
   * Hashcode of the value stored in text and a unique key to each text
   * description and it's associated ner record.
   */
  @Column(name = "hash", nullable = false)
  @EqualsAndHashCode.Include
  private int hash;

  /**
   * Text description associated with one or more vulnerabilities.
   */
  @Basic
  @EqualsAndHashCode.Exclude
  private String text;

  /**
   * Results of performing name entity recognition on the text description of
   * a vulnerability.
   */
  @Basic
  @EqualsAndHashCode.Exclude
  private String ner;
}
