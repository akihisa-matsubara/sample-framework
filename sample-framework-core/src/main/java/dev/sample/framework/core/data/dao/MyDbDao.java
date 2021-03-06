package dev.sample.framework.core.data.dao;

import dev.sample.framework.core.data.entitymanager.MyDb;
import java.io.Serializable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import lombok.Getter;

/**
 * mydb汎用Dao.
 *
 * @param <E> Entity
 * @param <PK> Primary Key
 */
@Getter
public abstract class MyDbDao<E, PK extends Serializable> extends GenericDao<E, PK> {

  @Inject
  @MyDb
  private EntityManager entityManager;

}
