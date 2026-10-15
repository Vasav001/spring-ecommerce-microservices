#!/usr/bin/env bash
set -euo pipefail
read -r -p "Delete all shop data (products, orders, stock, reviews, profiles) on the live cluster? Type yes: " answer
[ "$answer" = yes ] || exit 1
for db in customer_db catalog_db inventory_db order_db integration_db search_db; do
  kubectl -n ecom exec postgres-0 -- psql -U postgres -d "$db" -v ON_ERROR_STOP=1 -qc "
    DO \$\$ DECLARE t text; BEGIN
      FOR t IN SELECT quote_ident(tablename) FROM pg_tables WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history' LOOP
        EXECUTE 'TRUNCATE ' || t || ' CASCADE';
      END LOOP;
    END \$\$;"
  echo "emptied $db"
done
kubectl -n ecom exec deploy/redis -- redis-cli FLUSHALL >/dev/null
echo "flushed redis; Keycloak users are untouched"
