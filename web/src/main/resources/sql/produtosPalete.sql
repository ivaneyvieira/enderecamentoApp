select quantMov, COUNT(*) as qt, max(dataHoraMov) as data
from transferencias
where idMovProduto in (select id
                        from movprodutos
                        where idProduto in (select id
                                             from produtos
                                             where prdno = :prdno))
  and idEnderecoSai = 1
GROUP BY quantMov
ORDER BY qt desc, data desc
LIMIT 1