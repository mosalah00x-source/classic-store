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
    public <X> ValueBinder<X> getBinder(final JavaType<X> javaType) {
        return new BasicBinder<X>(javaType, this) {
            @Override
            protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options)
                    throws SQLException {
                String str = javaType.unwrap(value, String.class, options);
                if (str == null) {
                    st.setNull(index, Types.OTHER);
                } else {
                    st.setObject(index, str, Types.OTHER);
                }
            }

            @Override
            protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
                    throws SQLException {
                String str = javaType.unwrap(value, String.class, options);
                if (str == null) {
                    st.setNull(name, Types.OTHER);
                } else {
                    st.setObject(name, str, Types.OTHER);
                }
            }
        };
    }

    @Override
    public <X> ValueExtractor<X> getExtractor(final JavaType<X> javaType) {
        return new BasicExtractor<X>(javaType, this) {
            @Override
            protected X doExtract(ResultSet rs, int paramIndex, WrapperOptions options)
                    throws SQLException {
                return javaType.wrap(rs.getString(paramIndex), options);
            }

            @Override
            protected X doExtract(CallableStatement statement, int index, WrapperOptions options)
                    throws SQLException {
                return javaType.wrap(statement.getString(index), options);
            }

            @Override
            protected X doExtract(CallableStatement statement, String paramName, WrapperOptions options)
                    throws SQLException {
                return javaType.wrap(statement.getString(paramName), options);
            }
        };
    }
}
