-- Verificar imagens associadas ao documento 51
SELECT count(*) FROM documento_imagens WHERE documento_id = 51;

-- Verificar se há entradas em imagem_ocr_resultado (embora improvável se parou antes)
SELECT count(*) FROM imagem_ocr_resultado WHERE documento_id = 51;

-- Ver se a URL da thumbnail ou preview foi preenchida
SELECT url_thumbnail, url_preview FROM documentos WHERE id = 51;
