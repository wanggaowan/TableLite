### 更新日志
- v1.17
  >TextCellDraw部分私有方法改为protected，允许子类覆写
  >CellFactory回调增加总行数和总列数参数
  >TouchHelper增加单元格长按监听及其它判断滑动相关方法

- v1.16
  >增加CellTouchListener监听，可得到单元格更精细的触摸事件回调

- v1.15
  >修复预览时无法初始化问题

- v1.14
  > * 增加滑动监听
  > * 修复滑动到顶部和底部时，请求父类不要拦截的Bug
  > * 移除SurfaceTable

- v1.13
  >TouchHelper新增设置拖拽监听功能

- v1.11
  >修复刷新数据时数据可能不绘制Bug。如果刷新之前表格数据有大量数据且用户向上滑动或向左滑动，在刷新后数据量较小，由于之前滑动位置没有重置，可能导致数据没有绘制到界面，此时需要用户主动滑动一次表格

- v1.9
  > 修复Table被修改ScrollX和ScrollY时绘制异常Bug，因此禁止外部调用setScrollY、setScrollX和scrollTo方法（这三个方法是空实现），如果要代码滑动表格，可调用TouchHelper中对应的方法

- v1.8
  > * Table新增setNewData、clearData、setCellClickListener三个方法，
  当然你也可以通过获取TableData和TouchHelper实例来调用相关方法
  > * TableConfig新增isBothHighLightRowAndColumn配置项，用于是否需要同时高亮选 中的行和列
  > * 修复TableConfig中isHighLightSelectRow和isHighLightSelectColumn两个配置项产生的效果刚好相反Bug（粗心了，坏笑）

- v1.7
  > * 修复多线程数据同步问题
  > * 暂停SurfaceTable使用，关键问题未解决

- v1.6
  > * TableConfig新增dragHighLightColor属性配置，当拖拽改变行高或列宽时高亮显示行或列
  > * 修复拖拽改变行高或列宽时滑动冲突Bug
  > * 修复当高亮列后，此时去拖拽改变行高，高亮列绘制改变大小指示器Bug，反之亦然

- v1.5
  > * 修复TableConfig中setRowDragChangeHeightType方法与setColumnDragChangeWidthType方法效果设置刚好相反Bug
  > * 去除当设置点击高亮或拖拽改变行高列宽时默认固定第一行或第一列设定，现在是否固定完全由使用者自行决定
  > * TableConfig中增加拖拽改变行高或列宽后是否需要恢复之前高亮内容配置

- v1.4
  > 修复TableConfig 配置mColumnDragIndicatorSize无效Bug

- v1.3 重大更改
  > * CellFactory获取的对象修改为继承Cell的泛型，相关类都加入泛型对象
  > * 修复删除行列时如果什么都不传导致空指针异常Bug
  > * 修复触摸冲突Bug

- v1.2
  > 修复拖拽改变列宽或行高后，如果数据有变更，当列宽或行高设置为TableConfig.INVALID_VALUE(自适应)
  时列宽或行高根据内容也相应变更大小，不能保持拖拽后列宽或行高Bug

- v1.1
  > 修改文档

- v1.0
  > 正式版发布,快速生成表格，支持行列固定，高亮选中行列，拖拽改变列宽行高等

