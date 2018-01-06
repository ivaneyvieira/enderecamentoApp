select invno, nfname, invse, cast(inv.date as date) as data,
       vend.name fornecedor, vend.cgc as cnpj,
       CONCAT(TRIM(LEADING '0' FROM TRIM(nfname)), '/', TRIM(LEADING '0' FROM TRIM(invse))) as documento
from inv
  inner join vend
    on vend.no = inv.vendno
where nfname = :nfname
  AND (invse  = :invse OR :invse = '')
  AND inv.storeno = 10
  AND bits & POW(2, 4) = 0
  AND bits & POW(2, 6) = 0
