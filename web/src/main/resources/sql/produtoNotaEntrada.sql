select iprd.invno, iprd.prdno, TRIM(MID(prd.name, 1, 38)) as nomeProduto,
       iprd.grade, (iprd.qtty/1000) quant, MAX(trim(S.barcode)) as codbar
from iprd
  inner join prd
    ON iprd.prdno = prd.no
  left join sqlpdv.prdstk AS S
    ON S.prdno = iprd.prdno
   AND S.grade = iprd.grade
   AND S.storeno IN (1,2,3,4,5,6,7,8,9,10,11,12)
where invno = :invno
GROUP BY invno, prdno, grade