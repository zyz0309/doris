// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import org.codehaus.groovy.runtime.IOGroovyMethods

suite ("test_dup_mv_plus") {
    sql """ DROP TABLE IF EXISTS d_table; """

    sql """
            create table d_table(
                k1 int null,
                k2 int not null,
                k3 bigint null,
                k4 varchar(100) null
            )
            duplicate key (k1,k2,k3)
            distributed BY hash(k1) buckets 3
            properties("replication_num" = "1");
        """

    sql "insert into d_table select 1,1,1,'a';"
    sql "insert into d_table select 1,1,1,'a';"
    sql "insert into d_table select 2,2,2,'b';"
    sql "insert into d_table select 2,2,2,'b';"
    sql "insert into d_table select 3,-3,null,'c';"
    sql "insert into d_table select 3,-3,null,'c';"

    createMV ("create materialized view k12p as select k1,k2+1 from d_table;")

    sql "insert into d_table select -4,-4,-4,'d';"
    sql "insert into d_table select -4,-4,-4,'d';"

    sql "analyze table d_table with sync;"
    sql """alter table d_table modify column k4 set stats ('row_count'='8');"""
    sql """set enable_stats=false;"""

    qt_select_star "select * from d_table order by k1;"

    mv_rewrite_success("select k1,k2+1 from d_table order by k1;", "k12p")
    qt_select_mv "select k1,k2+1 from d_table order by k1;"

    mv_rewrite_success("select k2+1 from d_table order by k1;", "k12p")
    qt_select_mv_sub "select k2+1 from d_table order by k1;"

    mv_rewrite_success("select k2+1 from d_table order by k1+1-1;", "k12p")
    qt_select_mv_sub_add "select k2+1-1 from d_table order by k1+1-1;"

    mv_rewrite_success("select sum(k2+1) from d_table group by k1 order by k1;", "k12p")
    qt_select_group_mv "select sum(k2+1) from d_table group by k1 order by k1;"

    mv_rewrite_success("select sum(k1) from d_table group by k2+1 order by k2+1;", "k12p")
    qt_select_group_mv "select sum(k1) from d_table group by k2+1 order by k2+1;"

    mv_rewrite_success("select sum(k1+1-1) from d_table group by k2+1 order by k2+1;", "k12p")
    qt_select_group_mv_add "select sum(k1+1-1) from d_table group by k2+1 order by k2+1;"

    mv_rewrite_fail("select sum(k2) from d_table group by k3;", "k12p")
    qt_select_group_mv_not "select sum(k2) from d_table group by k3 order by k3;"

    mv_rewrite_fail("select k1,k2+1 from d_table order by k2;", "k12p")
    qt_select_mv "select k1,k2+1 from d_table order by k2;"

    sql """set enable_stats=true;"""
    mv_rewrite_success("select k1,k2+1 from d_table order by k1;", "k12p")

    mv_rewrite_success("select k2+1 from d_table order by k1;", "k12p")

    mv_rewrite_success("select k2+1 from d_table order by k1+1-1;", "k12p")

    mv_rewrite_success("select sum(k2+1) from d_table group by k1 order by k1;", "k12p")

    mv_rewrite_success("select sum(k1) from d_table group by k2+1 order by k2+1;", "k12p")

    mv_rewrite_success("select sum(k1+1-1) from d_table group by k2+1 order by k2+1;", "k12p")

    mv_rewrite_fail("select sum(k2) from d_table group by k3;", "k12p")

    mv_rewrite_fail("select k1,k2+1 from d_table order by k2;", "k12p")

}
