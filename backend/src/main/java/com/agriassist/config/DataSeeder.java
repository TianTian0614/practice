package com.agriassist.config;

import com.agriassist.entity.PriceQuote;
import com.agriassist.entity.TraceRecord;
import com.agriassist.repository.PriceQuoteRepository;
import com.agriassist.repository.TraceRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 演示数据种子：首次启动时自动灌入
 * 1) 农产品价格行情（5 产品 × 4 渠道 × 近 14 天）
 * 2) 可信溯源批次（3 个批次完整流转链）
 * 仅在对应表为空时执行，不影响已有数据。
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final PriceQuoteRepository priceRepo;
    private final TraceRecordRepository traceRepo;

    public DataSeeder(PriceQuoteRepository priceRepo, TraceRecordRepository traceRepo) {
        this.priceRepo = priceRepo;
        this.traceRepo = traceRepo;
    }

    @Override
    public void run(String... args) {
        // 价格行情改用真实数据：见 collect_real_prices.py（从农业农村部官方市场行情库采集，可溯源）。
        // 不再在此造假的确定性价格，以免与毕设"真实有效可获取"要求冲突。
        seedTraces();
    }

    /**
     * 价格行情：已改为"真实数据采集"模式，由 collect_real_prices.py 从农业农村部
     * 官方市场行情库（pfsc.agri.cn）抓取真实带来源的价格区间写入 price_quote，
     * 不再在此生成确定性假数据。故此处留空。
     */
    private void seedPrices() {
        // 真实数据统一通过脚本采集，保留空实现避免误灌假数据。
    }

    private void seedTraces() {
        if (traceRepo.count() > 0) return;

        // 说明：以下为「系统演示数据」，用于展示农产品溯源链路机制。
        // 真实运行环境应由生产主体/加工/物流/销售各环节真实录入，并对接第三方认证。
        // 演示批次均选用本系统已具备真实价格数据的产品（苹果/葡萄/黄花菜），保证演示内部一致。
        String[][][] batches = {
                {   // 批次1：红富士苹果（与 price_quote 中真实价格数据对应）
                        {"APL-20261006-001", "红富士苹果", "种植基地管理", "果园统一水肥管理，全程记录农事操作，未使用违禁农药", "山西运城临猗县果园基地", "果农专业合作社"},
                        {"APL-20261006-001", "红富士苹果", "采摘分拣", "人工采摘后按果径 80mm+/75mm+ 两级分拣，剔除伤果", "山西运城临猗县果园基地", "采摘班组"},
                        {"APL-20261006-001", "红富士苹果", "质量检测", "农残快检合格（抑制率<15%），糖度抽检 14.5°Brix", "县农产品质检站", "质检员"},
                        {"APL-20261006-001", "红富士苹果", "冷链配送", "全程 4℃ 冷链运输，运输时长 18 小时", "冷链物流车 JS-036", "物流公司"},
                        {"APL-20261006-001", "红富士苹果", "到货上架", "到达批发市场并完成平台预售上架", "太原河西农副产品批发市场", "平台运营"}
                },
                {   // 批次2：阳光玫瑰葡萄（与 price_quote 中真实价格数据对应）
                        {"GRP-20260928-002", "阳光玫瑰葡萄", "园区种植", "避雨栽培，水肥一体化滴灌，套袋防病", "山西晋中葡萄种植园", "种植户"},
                        {"GRP-20260928-002", "阳光玫瑰葡萄", "成熟采摘", "糖度≥18°Brix 分批采收，清晨低温时段采摘", "山西晋中葡萄种植园", "采摘班组"},
                        {"GRP-20260928-002", "阳光玫瑰葡萄", "预冷分选", "采后 2 小时内预冷，按串型与糖度分级", "产地预冷分选中心", "加工厂"},
                        {"GRP-20260928-002", "阳光玫瑰葡萄", "质检封装", "农残检测合格，保鲜盒+冰袋封装", "县农产品质检站", "质检员"},
                        {"GRP-20260928-002", "阳光玫瑰葡萄", "冷链发运", "0~4℃ 冷链发往销地仓，生成溯源二维码", "冷链物流车 JS-041", "物流公司"}
                },
                {   // 批次3：大同黄花菜（与官方「大同黄花价格指数」对应）
                        {"HHC-20261002-003", "大同黄花菜", "基地种植", "大同盆地盐碱地改良种植，绿色标准化生产", "山西大同云州区种植基地", "合作社"},
                        {"HHC-20261002-003", "大同黄花菜", "适时采摘", "凌晨采收未开放花蕾，保证品质与口感", "山西大同云州区种植基地", "采摘班组"},
                        {"HHC-20261002-003", "大同黄花菜", "蒸制杀青", "鲜蕾蒸汽杀青后自然晾晒至含水率 12%", "云州区黄花加工车间", "加工厂"},
                        {"HHC-20261002-003", "大同黄花菜", "质检分装", "二氧化硫残留未检出，分级真空包装", "县农产品质检站", "质检员"},
                        {"HHC-20261002-003", "大同黄花菜", "入库待售", "成品入库，同步至平台预售通道", "县城仓储中心 A-03 库位", "仓储管理员"}
                }
        };

        List<TraceRecord> records = new ArrayList<>();
        LocalDateTime base = LocalDateTime.now().with(LocalTime.of(9, 0));
        for (String[][] batch : batches) {
            for (int i = 0; i < batch.length; i++) {
                TraceRecord r = new TraceRecord();
                r.setBatchNo(batch[i][0]);
                r.setProductName(batch[i][1]);
                r.setStep(batch[i][2]);
                r.setDetail(batch[i][3]);
                r.setLocation(batch[i][4]);
                r.setOperator(batch[i][5]);
                r.setOccurredAt(base.minusDays(batch.length - i).plusHours(i * 6L));
                r.setIsDemo(1); // 标记为系统演示数据
                records.add(r);
            }
        }
        traceRepo.saveAll(records);
    }
}
