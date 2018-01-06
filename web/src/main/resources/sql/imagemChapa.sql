select F.CHAPA, I.IMAGEM
from PPESSOA AS P
  inner join PFUNC AS F
    on F.CODPESSOA = P.CODIGO
  inner join GIMAGEM AS I
    ON P.IDIMAGEM = I.ID
where F.CHAPA = :chapa