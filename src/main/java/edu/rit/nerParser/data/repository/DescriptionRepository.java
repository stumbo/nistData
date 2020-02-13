package edu.rit.nerParser.data.repository;

import edu.rit.nerParser.data.DescriptionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Description Repository
 *
 * @author wstumbo
 */
@Repository
public interface DescriptionRepository extends CrudRepository<DescriptionEntity, Integer> {

  /**
   * Find a description entity by the the provided Hashcode
   * @param hashcode to search using
   * @return Description entity matching hashcode or Optional.ofEmpty
   */
  Optional<DescriptionEntity> findFirstByHash(Integer hashcode);

  /**
   * Get all description entities that need Name Entity Recognition
   *
   * @return all descriptions entities still needing NER
   */
  Iterable<DescriptionEntity> getAllByNerIsNull();
}
