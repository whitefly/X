package center.utils;

import center.exception.WebException;
import com.entity.*;
import com.utils.FieldUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static center.exception.ErrorCode.*;

public class TaskCheckerUtil {
    public static void checkParserInfo(NewsParserDO parser) {
        //验证extra的格式
        checkExtrasIsValid(parser.getExtra());

        //根据配置类型不同 分开进行检查
        if (parser instanceof IndexParserDO) {
            //目录定位不能为空
            IndexParserDO indexParserDO = (IndexParserDO) parser;
            if (checkFieldsNotHasLocator(indexParserDO.getIndexRule())) {
                throw new WebException(SERVICE_PARSER_FIELD_LOCATOR_MISS);
            }
        }

        if (parser instanceof PageParserDO) {
            PageParserDO parserDO = (PageParserDO) parser;
            if (!checkParamNotEmpty(parserDO.getPageRule())) {
                throw new WebException(SERVICE_PARSER_FIELD_LOCATOR_MISS);
            }
        }

        if (parser instanceof EpaperParserDO) {
            EpaperParserDO parserDO = (EpaperParserDO) parser;
            if (!checkParamNotEmpty(parserDO.getLayoutRule())) {
                throw new WebException(SERVICE_PARSER_FIELD_LOCATOR_MISS);
            }
        }

        //处理自定义流程的合法性
        if (parser instanceof CustomParserDO) {
            CustomParserDO parserDO = (CustomParserDO) parser;
            List<StepDO> customRule = parserDO.getCustomRule();

            for (StepDO step : customRule) {
                boolean isValid = checkStepIsValid(step);
                if (!isValid) throw new WebException(SERVICE_PARSER_CUSTOM_ERROR);
            }
        }
    }

    private static void checkExtrasIsValid(List<ExtraField> fields) {
        //允许不设定额外属性
        if (CollectionUtils.isEmpty(fields)) return;

        for (ExtraField f : fields) {
            checkExtraIsValid(f);
        }

        //验证是否有重名
        Set<String> set = fields.stream().map(FieldDO::getName).collect(Collectors.toSet());
        if (fields.size() != set.size()) throw new WebException(SERVICE_PARSER_FIELD_DUP);
    }

    private static void checkExtraIsValid(ExtraField f) {
        //属性名不能为空
        if (StringUtils.isEmpty(f.getName())) throw new WebException(SERVICE_PARSER_MISS_FIELD_NAME);

        //定位器可以为空,会使用全局范围

        //类别不能为空
        if (f.getExtraType() == null) throw new WebException(SERVICE_PARSER_MISS_FIELD_TYPE);
    }

    public static boolean checkParamNotEmpty(Object... params) {
        return Arrays.stream(params).allMatch(x -> {
            if (x instanceof FieldDO) {
                return !FieldUtil.hasNoLocator((FieldDO) x);
            } else {
                return x != null;
            }
        });
    }

    public static boolean checkFieldsNotHasLocator(FieldDO... fieldDOS) {
        return Arrays.stream(fieldDOS).allMatch(FieldUtil::hasNoLocator);
    }

    public static boolean checkStepIsValid(StepDO step) {
        if (step == null) {
            return false;
        }

        //一定需要有别名
        if (org.springframework.util.StringUtils.isEmpty(step.getAlias())) {
            return false;
        }

        //若使用了采集,则可以不用分支
        if (!step.isExtract() && org.springframework.util.CollectionUtils.isEmpty(step.getLinks())) {
            return false;
        }

        //每个分支要合法
        return step.getLinks().stream().allMatch(TaskCheckerUtil::checkAliasFieldIsValid);
    }

    public static boolean checkAliasFieldIsValid(AliasField f) {
        if (f == null) return false;

        //定位器要有
        if (FieldUtil.hasNoLocator(f)) return false;

        //分支上的别名要有
        return !org.springframework.util.StringUtils.isEmpty(f.getAlias());
    }
}
