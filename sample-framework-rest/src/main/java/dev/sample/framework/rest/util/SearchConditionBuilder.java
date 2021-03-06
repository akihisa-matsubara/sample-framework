package dev.sample.framework.rest.util;

import dev.sample.framework.core.data.condition.FilterDo;
import dev.sample.framework.core.data.condition.SearchConditionDo;
import dev.sample.framework.core.data.condition.SortDo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 検索条件DOビルダー.
 */
@NoArgsConstructor
@AllArgsConstructor
public class SearchConditionBuilder {

  /** Offset(default: 0). */
  private int offset = 0;

  /** 取得件数(default: 100). */
  private int limit = 100;

  /** ソート条件((default: empty string)). */
  private String sort = StringUtils.EMPTY;

  /** クエリパラメータMap. */
  private final Map<String, Object> queryParams = new LinkedHashMap<>();

  /**
   * クエリパラメータMapにパラメーターを格納します.
   * 格納順番がそのままwhere節の出力順となる為、アクセスパスを考慮して格納すること.
   *
   * @param column 項目
   * @param value 値
   * @return クエリビルダー
   */
  public SearchConditionBuilder putParam(String column, Object value) {
    if (value != null) {
      queryParams.put(column, value);
    }
    return this;
  }

  /**
   * 検索条件DOの作成.
   *
   * @return {@link SearchConditionDo} 検索条件DO
   */
  public SearchConditionDo build() {
    return SearchConditionDo.builder()
        .filter(FilterDo.builder().offset(offset).limit(limit).build())
        .sortList(createSortDos(sort))
        .queryParams(queryParams)
        .build();
  }

  /**
   * ソート順DOの作成.
   * fieldName[:asc or desc],fieldName...形式のソート文字列を分解し、ソート順DOを作成します.
   * (変更案：asc,descを +(asc) or -(desc) で実装するのもあり)
   *
   * @param sortStr ソート文字列
   * @return {@link SortDo}のリスト
   */
  private List<SortDo> createSortDos(String sortStr) {
    List<SortDo> sortDoList = new ArrayList<>();
    sortStr = StringUtils.remove(sortStr, StringUtils.SPACE);

    if (StringUtils.isEmpty(sortStr)) {
      return sortDoList;
    }

    for (String sortPhrase : StringUtils.split(sortStr, ",")) {
      String[] sortWords = StringUtils.split(sortPhrase, ":");
      if (sortWords.length == 1) {
        sortDoList.add(SortDo.builder().field(sortWords[0]).build());

      } else {
        sortDoList.add(SortDo.builder().field(sortWords[0]).asc(!"desc".equalsIgnoreCase(sortWords[1])).build());

      }
    }

    return sortDoList;
  }

}
