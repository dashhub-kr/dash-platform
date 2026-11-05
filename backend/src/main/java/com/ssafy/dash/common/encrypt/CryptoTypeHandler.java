package com.ssafy.dash.common.encrypt;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(String.class)
public class CryptoTypeHandler extends BaseTypeHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(CryptoTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        String encrypted = AesEncryptor.getInstance().encrypt(parameter);
        ps.setString(i, encrypted);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decryptOrNull(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decryptOrNull(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decryptOrNull(cs.getString(columnIndex));
    }

    /**
     * 복호화할 수 없는 값은 사용 불가로 보고 null을 반환한다 (예: 토큰 → 재로그인 유도).
     */
    private String decryptOrNull(String encrypted) {
        try {
            return AesEncryptor.getInstance().decrypt(encrypted);
        } catch (AesEncryptor.CryptoException e) {
            log.warn("Failed to decrypt column value; treating as unavailable");
            return null;
        }
    }
}
