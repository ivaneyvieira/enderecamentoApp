DROP TABLE IF EXISTS A1;
CREATE TEMPORARY TABLE A1
(primary keY(tipoAltura))
    select 'ALTA' as tipoAltura, 22 as peso
    union
    select 'MEDIA', 18
    union
    select 'BAIXA', 10;

DROP TABLE IF EXISTS A2;
CREATE TEMPORARY TABLE A2
    SELECT * from A1
    where tipoAltura = :tipoAltura;

DROP TABLE IF EXISTS A3;
CREATE TEMPORARY TABLE A3
(PRIMARY KEY(tipoAltura))
    select A1.tipoAltura, A1.peso - A2.peso +1 as peso
    FROM A1
      INNER JOIN A2
        ON A1.peso >= A2.peso;


DROP TABLE IF EXISTS T;
CREATE TEMPORARY TABLE T
(PRIMARY KEY(rua, predio, nivel, apto))
    select E.id as idEndereco,
           r.id as idRua,
           CONCAT(r.numero, '-', p.numero, '-', n.numero, '-', a.numero) as endereco,
      tipoPalet,
      tipoAltura,
           r.numero as rua,
           p.numero as predio,
           n.numero as nivel,
           a.numero as apto,
           IF(SUM(saldoConfirmado) > 0 OR SUM(saldoNConfirmado) > 0, tipoPalet, '.') as ocupacao,
      A3.peso
    from enderecos AS E
      inner join aptos AS a
        ON a.idEndereco = E.id
      inner join niveis AS n
        ON n.id = a.idNivel
      inner join A3
      using(tipoAltura)
      inner join predios AS p
        ON p.id = n.idPredio
      inner join ruas AS r
        ON r.id = p.idRua
      left join saldos AS S
        on E.id = S.idEndereco
    where n.tipoNivel = 'PULMAO'
          AND idRua IN (:ruas)
    GROUP BY endereco;

DROP TABLE IF EXISTS T2;
CREATE TEMPORARY TABLE T2
(INDEX(paletes))
    select * from (
                    select 'P' as paletes, '02' as apto, 'P' as tipoPalet, 1 as peso from dual
                    union
                    select 'P' as paletes, '02' as apto, 'T', 1 as peso from dual
                    union
                    select 'P' as paletes, '02' as apto, 'G', 2 as peso from dual

                    union
                    select '.P' as paletes, '01' as apto, 'P', 4 as peso from dual
                    union
                    select '.P' as paletes, '03' as apto, 'P', 6 as peso from dual
                    union
                    select '..P' as paletes, '02' as apto, 'P', 4 as peso from dual
                    union
                    select '..P' as paletes, '01' as apto, 'T', 6 as peso from dual
                    union
                    select '..P' as paletes, '01' as apto, 'G', 2 as peso from dual

                    union
                    select '.G' as paletes, '01' as apto, 'G', 1 as peso from dual
                    union
                    select '.G' as paletes, '01' as apto, 'P', 2 as peso from dual

                    union
                    select 'G' as paletes, '02' as apto, 'G', 1 as peso from dual
                    union
                    select 'G' as paletes, '02' as apto, 'P', 2 as peso from dual

                    union
                    select 'T' as paletes, '02' as apto, 'P', 1 as peso from dual
                    union
                    select '.T' as paletes, '01' as apto, 'P', 1 as peso from dual

                    union
                    select '' as paletes, '01' as apto, 'P', 1 as peso from dual
                    union
                    select '' as paletes, '01' as apto, 'G', 1 as peso from dual
                    union
                    select '' as paletes, '01' as apto, 'T', 1 as peso from dual
                    union
                    select '' as paletes, '01' as apto, 'X', 1 as peso from dual
                  ) as D where tipoPalet = :tipoPalet;

DROP TABLE IF EXISTS T3;
CREATE TEMPORARY TABLE T3
(INDEX(paletes))
    select rua, predio, nivel,
      CAST(TRIM(TRAILING  '.' FROM GROUP_CONCAT(ocupacao order by apto separator '')) AS CHAR) as paletes
    from T
    GROUP BY rua, predio, nivel;

select E.*,  T.peso*T2.peso as recomendado, T.peso, T2.peso, T.tipoAltura,
  paletes, T.tipoPalet
from T3
  inner join T2
  USING(paletes)
  inner join T
  USING(rua, predio, nivel, apto)
  inner join enderecos AS E
    ON E.id = T.idEndereco
GROUP BY E.id
order by recomendado, endereco