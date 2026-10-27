package com.classicstore.identity.domain;

import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.BasicBinder;
import org.hibernate.type.descriptor.jdbc.BasicExtractor;
import org.hibernate.type.descriptor.jdbc.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class CitextJdbcType implements JdbcType {

    public static final CitextJdbcType INSTANCE = new CitextJdbcType();

    @Override
    public int getJdbcTypeCode() {
        return Types.OTHER;
    }

    @Override
    public <X> ValueBinder<X> getBinder(JavaType<X> javaType) {
        return new BasicBinder<>(javaType, this) {
            @Override
            protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
                st.setObject(index, javaType.unwrap(value, String.class, options), Types.OTHER);
            }

            @Override
            protected void doBind(CallableStatement st, X value, String name, WrapperOptions options) throws SQLException {
                st.setObject(name, javaType.unwrap(value, String.class, options), Types.OTHER);
            }
        };
    }

    @Override
    public <X> ValueExtractor<X> getExtractor(JavaType<X> javaType) {
        return new BasicExtractor<>(javaType, this) {
            @Override
            protected X doExtract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException {
                return javaType.wrap(rs.getString(paramIndex), options);
            }

            @Override
            protected X doExtract(CallableStatement st, int index, WrapperOptions options) throws SQLException {
                return javaType.wrap(st.getString(index), options);
            }

            @Override
            protected X doExtract(CallableStatement st, String name, WrapperOptions options) throws SQLException {
                return javaType.wrap(st.getString(name), options);
            }
        };
    }
}
