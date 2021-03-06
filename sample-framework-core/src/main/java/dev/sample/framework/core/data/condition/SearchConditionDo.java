package dev.sample.framework.core.data.condition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * 検索条件DO.
 */
@Builder
@Data
public class SearchConditionDo {

  /** フィルターDO. */
  @Builder.Default
  private FilterDo filter = FilterDo.builder().build();

  /** ソートキーのリスト. */
  @Builder.Default
  private List<SortDo> sortList = new ArrayList<>();

  /** クエリパラメーターMap. */
  @Builder.Default
  private Map<String, Object> queryParams = new LinkedHashMap<>();

  /** 検索件数を取得するためのクエリ. */
  private String countQuery;

  /** 検索結果を取得するためのクエリ. */
  private String searchQuery;

}
