-- KEYS[1]: Rate limit Redis key (e.g., ratelimit:{client_id})
-- ARGV[1]: Max bucket capacity
-- ARGV[2]: Refill rate per millisecond
-- ARGV[3]: Requested tokens (usually 1)
-- ARGV[4]: Current timestamp (epoch milliseconds)

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate_per_ms = tonumber(ARGV[2])
local requested = tonumber(ARGV[3])
local now = tonumber(ARGV[4])

-- Retrieve current bucket state
local data = redis.call("HMGET", key, "tokens", "last_updated")
local tokens = tonumber(data[1])
local last_updated = tonumber(data[2])

if tokens == nil then
    tokens = capacity
    last_updated = now
else
    local elapsed = math.max(0, now - last_updated)
    tokens = math.min(capacity, tokens + (elapsed * refill_rate_per_ms))
    last_updated = now
end

if tokens >= requested then
    tokens = tokens - requested
    redis.call("HMSET", key, "tokens", tokens, "last_updated", last_updated)
    -- Expire bucket if inactive for 1 hour
    redis.call("EXPIRE", key, 3600)
    -- Return: 1 (allowed), remaining tokens, retry_after_ms (0)
    return {1, math.floor(tokens), 0}
else
    redis.call("HMSET", key, "tokens", tokens, "last_updated", last_updated)
    local missing = requested - tokens
    local wait_ms = math.ceil(missing / refill_rate_per_ms)
    return {0, math.floor(tokens), wait_ms}
end