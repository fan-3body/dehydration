# dehydration

`dehydration` 是一个面向 Forge 1.20.1 的小型模组，用来给玩家加入“三体脱水”状态，并和 Legendary Survival Overhaul 的口渴与温度系统联动。

## 功能

- 只有三种状态：`normal`、`dehydrated`、`rehydrating`
- 状态由服务端维护，客户端只负责 HUD、特效、音效和同步显示
- 不会新增第二条口渴条
- 进入 `dehydrated` 时会清空 LSO 口渴，并将最大生命上限压到 5 颗心
- 脱水时不会显示 LSO 的抗性增益或温度免疫效果
- 脱水时会屏蔽 LSO 的高温/低温屏幕遮罩和温度 HUD 抖动
- 脱水时会把 XP 条上方的 LSO 温度图标替换成脱水图标
- 只会拦截温度和口渴相关伤害，不会替换或屏蔽其他类型伤害
- 状态会通过 capability / NBT 持久化，登录、换维度、睡觉和死亡重生后都会保留

## 使用

- 游戏内默认按键是 `K`
- 按下后会向服务器发送切换请求
- 如果配置要求热射病才能进入脱水，那么只有在满足条件时才会允许进入

## 配置

服务端常见配置项：

- `requireHeatStrokeToEnter`
  - 是否必须处于热射病状态才能进入脱水
- `heatStrokeEffects`
  - 哪些 LSO 药水效果会被视为热射病
- `rehydrationTicks`
  - 进入复水状态后，需要保持多久才会恢复为 `normal`

## API

对外保持的稳定接口：

- `isDehydrated(Player player)`
- `getState(Player player)`
- `isFirstDehydration(Player player)`
- `isFirstRehydration(Player player)`

## 兼容性

- Minecraft: `1.20.1`
- Forge: `47.x`
- 可选联动：Legendary Survival Overhaul

## 构建

```bash
./gradlew build
```

构建完成后，模组产物会在 `build/libs/` 下。
