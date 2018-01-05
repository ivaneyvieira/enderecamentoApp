select distinct T.*
from transferencias AS T
  inner join enderecos AS ES
    ON T.idEnderecoSai = ES.id
  inner join enderecos AS EE
    ON T.idEnderecoEnt = EE.id
  inner join movprodutos AS M
    ON M.id = T.idMovProduto
where (ES.localizacao LIKE CONCAT(:rua, "%")
       OR EE.localizacao LIKE CONCAT(:rua, "%")
       OR :rua = '')
      and (M.idProduto = :produto OR :produto = 0)
      and (T.confirmacao = :confirmado OR :confirmado = '')
      and (T.idUser = :empilhador OR :empilhador = 0)

