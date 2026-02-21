-- 중복된 (address, detail_address) 조합에서 id가 가장 낮은 행만 남기고 나머지 삭제
DELETE FROM stores
WHERE id NOT IN (
    SELECT keep_id
    FROM (
        SELECT MIN(id) AS keep_id
        FROM stores
        GROUP BY address, detail_address
    ) AS t
);

-- 유니크 제약 추가
ALTER TABLE stores
    ADD CONSTRAINT uk_store_address UNIQUE (address, detail_address);
