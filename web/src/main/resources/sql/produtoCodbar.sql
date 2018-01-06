SELECT no as prdno, IFNULL(grade, '') as grade, TRIM(MID(P.name, 1, 38)) as nome,
  trim(IFNULL(B.barcode, P.barcode)) as codbar, P.clno,
  mfno as vendno, SUM(P.mult/1000) as quantVolumes, 0.0000 as estoqueMinimo,
  P.cost/10000 as custo, sp/100 as preco
FROM sqldados.prd AS P
  LEFT JOIN sqldados.prdbar AS B
    ON P.no = B.prdno
WHERE B.barcode = LPAD(:codbar, 16, ' ') OR P.barcode = LPAD(:codbar, 16, ' ')
GROUP BY prdno, grade