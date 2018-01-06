select invno, nfname, invse, cast(inv.date as date) as data,
       vend.name fornecedor, vend.cgc as cnpj,
       CONCAT(TRIM(LEADING '0' FROM TRIM(nfname)), '/', TRIM(LEADING '0' FROM TRIM(invse))) as documento
from inv
  inner join vend
    on vend.no = inv.vendno
where inv.storeno = 10
  AND bits & POW(2, 4) = 0
  AND bits & POW(2, 6) = 0
  AND cfo NOT IN (1253)
  AND date BETWEEN DATE_SUB(current_date, INTERVAL 90 DAY)*1 AND current_date*1
HAVING date_format(data, '%d/%m/%Y') like :query
     OR invno like :query
     OR nfname like :query
     OR fornecedor like :query
     OR cnpj like :query
     OR '' like :query
ORDER BY data desc, invno desc