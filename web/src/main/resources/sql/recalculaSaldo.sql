select idProduto, idEndereco, SUM(qtMovConf) as saldoConf, SUM(qtMovNConf) as saldoNConf
from (
select idProduto, idEnderecoSai as idEndereco,
  SUM(IF(T.confirmacao = 'Y', -T.quantMov, 0)) as qtMovConf,
  SUM(IF(T.confirmacao = 'Y', 0, -T.quantMov)) as qtMovNConf
  from movprodutos AS M
    inner join transferencias AS T
      ON M.id = T.idMovProduto
where idProduto = :idProduto
GROUP BY idProduto, idEnderecoSai
UNION
select idProduto, idEnderecoEnt,
  SUM(IF(T.confirmacao = 'Y', T.quantMov, 0)) as qtMovConf,
  SUM(IF(T.confirmacao = 'Y', 0, T.quantMov)) as qtMovNConf
  from movprodutos AS M
    inner join transferencias AS T
      ON M.id = T.idMovProduto
where idProduto = :idProduto
GROUP BY idProduto, idEnderecoEnt
) as D
group BY idProduto, idEndereco
