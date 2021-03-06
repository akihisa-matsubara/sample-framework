package dev.sample.framework.core.data.dao;

import dev.sample.framework.core.data.entity.SystemDateEntity;
import javax.enterprise.context.ApplicationScoped;

/**
 * システム日付マスタDao.
 */
@ApplicationScoped
public class SystemDateDao extends MyDbDao<SystemDateEntity, String> {

  /**
   * 1件取得.
   *
   * @return {@link SystemDateEntity} システム日付マスタEntity
   */
  public SystemDateEntity find() {
    return (SystemDateEntity) getEntityManager().createNamedQuery(SystemDateEntity.FIND).getSingleResult();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<SystemDateEntity> getEntityType() {
    return SystemDateEntity.class;
  }
}
